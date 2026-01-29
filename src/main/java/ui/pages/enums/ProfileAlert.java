package ui.pages.enums;

import lombok.Getter;

@Getter
public enum ProfileAlert {
    NAME_UPDATE_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_CANNOT_BE_SAME("⚠️ New name is the same as the current one.");

    private final String message;

    ProfileAlert(String message) {
        this.message = message;
    }
}
