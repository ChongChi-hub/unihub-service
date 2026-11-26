package com.unihub.identity.service;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.dto.UserImportPreviewResponse;
import com.unihub.identity.dto.UserImportPreviewResponse.ImportRow;
import com.unihub.identity.dto.UserImportPreviewResponse.RowError;
import com.unihub.identity.dto.UserImportResultResponse;
import com.unihub.identity.repository.RoleRepository;
import com.unihub.identity.repository.UserRepository;
import com.unihub.identity.security.GatewayUserPrincipal;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles CSV and Excel (.xlsx) bulk user import with row-level validation and
 * preview.
 */
@Service
public class UserImportService {

	private static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;

	public UserImportService(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditLogService = auditLogService;
	}

	public byte[] generateCsvTemplate() {
		String content = "email,fullName,code,roles,password\n"
				+ "sv001@unihub.edu.vn,Nguyen Van A,SV001,STUDENT,Student@123\n"
				+ "gv001@unihub.edu.vn,Tran Thi B,GV001,LECTURER,Lecturer@123\n";
		return content.getBytes(StandardCharsets.UTF_8);
	}

	public byte[] generateExcelTemplate() {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("users");
			Row header = sheet.createRow(0);
			String[] cols = {"email", "fullName", "code", "roles", "password"};
			for (int i = 0; i < cols.length; i++) {
				header.createCell(i).setCellValue(cols[i]);
			}
			Row sample = sheet.createRow(1);
			sample.createCell(0).setCellValue("sv001@unihub.edu.vn");
			sample.createCell(1).setCellValue("Nguyen Van A");
			sample.createCell(2).setCellValue("SV001");
			sample.createCell(3).setCellValue("STUDENT");
			sample.createCell(4).setCellValue("Student@123");
			workbook.write(out);
			return out.toByteArray();
		} catch (IOException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to generate Excel template");
		}
	}

	@Transactional(readOnly = true)
	public UserImportPreviewResponse preview(MultipartFile file) {
		List<ParsedRow> parsedRows = parseFile(file);
		return validateRows(parsedRows).preview();
	}

	@Transactional
	public UserImportResultResponse importUsers(GatewayUserPrincipal actor, MultipartFile file) {
		List<ParsedRow> parsedRows = parseFile(file);
		ValidationResult validation = validateRows(parsedRows);

		if (!validation.preview().errors().isEmpty()) {
			return new UserImportResultResponse(validation.preview().totalRows(), 0, validation.preview().errorCount(),
					validation.preview().errors());
		}

		List<UserEntity> toSave = new ArrayList<>();
		for (ValidatedRow row : validation.validEntities()) {
			UserEntity user = new UserEntity();
			user.setEmail(row.email());
			user.setFullName(row.fullName());
			user.setCode(row.code());
			user.setPasswordHash(passwordEncoder.encode(row.rawPassword()));
			user.setStatus(UserStatus.ACTIVE);
			user.setRoles(row.roles());
			toSave.add(user);
		}

		userRepository.saveAll(toSave);
		auditLogService.record(actor, "USER_IMPORTED", "USER", "BATCH", null, Map.of("importedCount", toSave.size()));

		return new UserImportResultResponse(validation.preview().totalRows(), toSave.size(), 0, List.of());
	}

	private ValidationResult validateRows(List<ParsedRow> parsedRows) {
		Map<String, RoleEntity> roleCache = new HashMap<>();
		for (RoleEntity role : roleRepository.findAll()) {
			roleCache.put(role.getName().toUpperCase(), role);
		}

		Set<String> seenEmails = new HashSet<>();
		Set<String> seenCodes = new HashSet<>();
		List<ImportRow> validPreviewRows = new ArrayList<>();
		List<ValidatedRow> validEntities = new ArrayList<>();
		List<RowError> errors = new ArrayList<>();

		for (ParsedRow row : parsedRows) {
			String email = row.email() == null ? "" : row.email().trim().toLowerCase();
			String fullName = row.fullName() == null ? "" : row.fullName().trim();
			String code = StringUtils.hasText(row.code()) ? row.code().trim() : null;
			String password = row.password() == null ? "" : row.password().trim();

			if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
				errors.add(new RowError(row.rowNumber(), email, "Invalid or missing email"));
				continue;
			}
			if (!StringUtils.hasText(fullName)) {
				errors.add(new RowError(row.rowNumber(), email, "Full name is required"));
				continue;
			}
			if (password.length() < 8) {
				errors.add(new RowError(row.rowNumber(), email, "Password must be at least 8 characters"));
				continue;
			}
			if (!seenEmails.add(email) || userRepository.existsByEmailIgnoreCase(email)) {
				errors.add(new RowError(row.rowNumber(), email, "Duplicate email"));
				continue;
			}
			if (code != null && (!seenCodes.add(code.toLowerCase()) || userRepository.existsByCodeIgnoreCase(code))) {
				errors.add(new RowError(row.rowNumber(), email, "Duplicate user code: " + code));
				continue;
			}

			Set<String> roleNames = parseRoles(row.rolesRaw());
			if (roleNames.isEmpty()) {
				errors.add(new RowError(row.rowNumber(), email, "At least one role is required"));
				continue;
			}

			Set<RoleEntity> resolvedRoles = new HashSet<>();
			String unknownRole = null;
			for (String roleName : roleNames) {
				RoleEntity entity = roleCache.get(roleName);
				if (entity == null) {
					unknownRole = roleName;
					break;
				}
				resolvedRoles.add(entity);
			}
			if (unknownRole != null) {
				errors.add(new RowError(row.rowNumber(), email, "Unknown role: " + unknownRole));
				continue;
			}

			validPreviewRows.add(new ImportRow(row.rowNumber(), email, fullName, code, roleNames));
			validEntities.add(new ValidatedRow(email, fullName, code, password, resolvedRoles));
		}

		UserImportPreviewResponse preview = new UserImportPreviewResponse(parsedRows.size(), validPreviewRows.size(),
				errors.size(), validPreviewRows, errors);
		return new ValidationResult(preview, validEntities);
	}

	private List<ParsedRow> parseFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Import file is empty");
		}
		if (file.getSize() > MAX_FILE_BYTES) {
			throw new AppException(ErrorCode.BAD_REQUEST, "File exceeds 5MB size limit");
		}
		String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
		if (filename.endsWith(".csv")) {
			return parseCsv(file);
		}
		if (filename.endsWith(".xlsx")) {
			return parseExcel(file);
		}
		throw new AppException(ErrorCode.BAD_REQUEST, "Only .csv and .xlsx files are supported");
	}

	private List<ParsedRow> parseCsv(MultipartFile file) {
		List<ParsedRow> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int rowNum = 0;
			while ((line = reader.readLine()) != null) {
				rowNum++;
				if (rowNum == 1 && line.toLowerCase().contains("email")) {
					continue;
				}
				if (!StringUtils.hasText(line)) {
					continue;
				}
				String[] parts = line.split(",", -1);
				rows.add(new ParsedRow(rowNum, getPart(parts, 0), getPart(parts, 1), getPart(parts, 2),
						getPart(parts, 3), getPart(parts, 4)));
			}
			return rows;
		} catch (IOException ex) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Failed to read CSV file");
		}
	}

	private List<ParsedRow> parseExcel(MultipartFile file) {
		List<ParsedRow> rows = new ArrayList<>();
		DataFormatter formatter = new DataFormatter();
		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			for (Row row : sheet) {
				int rowNum = row.getRowNum() + 1;
				String email = cellText(row.getCell(0), formatter);
				if (rowNum == 1 && email.equalsIgnoreCase("email")) {
					continue;
				}
				String fullName = cellText(row.getCell(1), formatter);
				if (!StringUtils.hasText(email) && !StringUtils.hasText(fullName)) {
					continue;
				}
				rows.add(new ParsedRow(rowNum, email, fullName, cellText(row.getCell(2), formatter),
						cellText(row.getCell(3), formatter), cellText(row.getCell(4), formatter)));
			}
			return rows;
		} catch (IOException ex) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Failed to read Excel file");
		}
	}

	private static String getPart(String[] parts, int index) {
		return index < parts.length ? parts[index].trim() : "";
	}

	private static String cellText(Cell cell, DataFormatter formatter) {
		return cell == null ? "" : formatter.formatCellValue(cell).trim();
	}

	private static Set<String> parseRoles(String rolesRaw) {
		if (!StringUtils.hasText(rolesRaw)) {
			return Set.of();
		}
		Set<String> result = new HashSet<>();
		Arrays.stream(rolesRaw.split("[;|]")).map(String::trim).filter(StringUtils::hasText).map(String::toUpperCase)
				.forEach(result::add);
		return result;
	}

	private record ParsedRow(int rowNumber, String email, String fullName, String code, String rolesRaw,
			String password) {
	}

	private record ValidatedRow(String email, String fullName, String code, String rawPassword, Set<RoleEntity> roles) {
	}

	private record ValidationResult(UserImportPreviewResponse preview, List<ValidatedRow> validEntities) {
	}
}
