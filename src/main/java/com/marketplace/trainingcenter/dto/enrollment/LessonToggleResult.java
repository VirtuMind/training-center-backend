package com.marketplace.trainingcenter.dto.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonToggleResult {
    private Long lessonId;
    private Boolean completed;
}
