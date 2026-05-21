package dev.mathuias.chessrepertoire.opening;

import java.time.Instant;

public record OpeningResponse(
        Long id,
        String name,
        String pgn,
        String initialFen,
        String notes,
        Instant createdAt
) {

    public static OpeningResponse from(Opening opening) {
        return new OpeningResponse(
                opening.getId(),
                opening.getName(),
                opening.getPgn(),
                opening.getInitialFen(),
                opening.getNotes(),
                opening.getCreatedAt()
        );
    }
}
