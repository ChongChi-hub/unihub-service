package com.unihub.common.exception;

/** Domain/application exception carrying a standardized ErrorCode. */
public class AppException extends RuntimeException {

	private final ErrorCode errorCode;

	public AppException(ErrorCode errorCode) {
		super(errorCode.getDefaultMessage());
		this.errorCode = errorCode;
	}

	public AppException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
