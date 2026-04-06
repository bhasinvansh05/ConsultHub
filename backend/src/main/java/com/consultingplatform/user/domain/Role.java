package com.consultingplatform.user.domain;

/**
 * Application roles. These match the single-table inheritance discriminator values
 * used by `User` subclasses (CONSULTANT, ADMIN, CLIENT).
 */
public enum Role {
    ADMIN,
    CONSULTANT,
    CLIENT
}
