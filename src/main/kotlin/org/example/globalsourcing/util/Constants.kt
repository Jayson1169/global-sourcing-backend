package org.example.globalsourcing.util

const val VARCHAR_MAX_SIZE: Int = 255
const val TEXT_MAX_SIZE: Int = 65535
const val MEDIUMTEXT_MAX_SIZE: Int = 16777215
const val LOG_MAX_SIZE: Int = 1023

const val HS_CODE_PATTERN: String = "^\\d{8}$"
const val EXPRESS_NUMBER_PATTERN: String = "^\\w\\d{7,17}$"
const val BARCODE_PATTERN: String = "^\\d{13}(\\d{2})?$"
const val USERNAME_PATTERN: String = "^[\\w-]{6,18}$"
const val PASSWORD_PATTERN: String = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\W]{6,18}$"
const val PHONE_NUMBER_PATTERN: String = "^1\\d{10}$"

const val RANDOM_BARCODE_LENGTH: Int = 15