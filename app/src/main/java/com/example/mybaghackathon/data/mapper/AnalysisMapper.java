package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.analysis.AnalysisResponseDto;
import com.example.mybaghackathon.data.remote.dto.analysis.RestrictedItemDto;
import com.example.mybaghackathon.model.AnalysisResult;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.model.TripSchedule;

import java.util.ArrayList;
import java.util.List;

// AnalysisResponseDto를 AnalysisResult 모델로 변환
public final class AnalysisMapper {

    private AnalysisMapper() {
    }

    public static AnalysisResult from(AnalysisResponseDto dto) {
        TripSchedule schedule = new TripSchedule(
                dto.getDestinationCountry(),
                dto.getDestinationCity(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getTransportMode(),
                dto.getIsOverseas()
        );

        return new AnalysisResult(
                dto.getAnalysisId(),
                schedule,
                dto.getAccommodationName(),
                mapRestrictedItems(dto.getRestrictedItems()),
                PackingItemMapper.from(dto.getRecommendedItems())
        );
    }

    private static List<RestrictedItem> mapRestrictedItems(List<RestrictedItemDto> dtos) {
        List<RestrictedItem> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (RestrictedItemDto dto : dtos) {
            result.add(new RestrictedItem(dto.getItemName(), dto.getRestrictionType(), dto.getReason()));
        }
        return result;
    }
}
