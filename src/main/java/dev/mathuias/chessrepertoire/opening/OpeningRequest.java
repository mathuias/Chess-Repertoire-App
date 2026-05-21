package dev.mathuias.chessrepertoire.opening;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpeningRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be 255 characters or fewer")
        String name,

        @NotBlank(message = "PGN is required")
        String pgn,

        @Size(max = 255, message = "Initial FEN must be 255 characters or fewer")
        String initialFen,

        String notes
) {
}
