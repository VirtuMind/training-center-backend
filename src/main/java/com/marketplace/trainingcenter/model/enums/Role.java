package com.marketplace.trainingcenter.model.enums;

public enum Role {
    ADMIN,
    TRAINER,
    STUDENT;
    
    public static Role fromUserRole(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        
        return Role.valueOf(userRole.name());
    }
}
