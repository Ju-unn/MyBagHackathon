package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.trip.TripDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripMemberDto;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TripDto/TripMemberDto를 Trip/TripMember 모델로 변환
public final class TripMapper {

    private TripMapper() {
    }

    public static Trip from(TripDto dto) {
        return from(dto, Collections.emptyList());
    }

    public static Trip from(TripDto dto, List<TripMemberDto> memberDtos) {
        return new Trip(
                dto.getTripId(),
                dto.getOwnerUserId(),
                dto.getTripName(),
                dto.getExpectedMemberCount(),
                dto.getTripType(),
                dto.getDestinationCountry(),
                dto.getDestinationCity(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getStatus(),
                dto.getCreatedAt(),
                fromMembers(memberDtos)
        );
    }

    public static List<Trip> fromList(List<TripDto> dtos) {
        List<Trip> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (TripDto dto : dtos) {
            result.add(from(dto));
        }
        return result;
    }

    public static TripMember fromMember(TripMemberDto dto) {
        return new TripMember(
                dto.getUserId(),
                dto.getNickname(),
                dto.getProfileImageUrl(),
                dto.getRole(),
                dto.getMemberStatus(),
                dto.getJoinedAt()
        );
    }

    public static List<TripMember> fromMembers(List<TripMemberDto> dtos) {
        List<TripMember> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (TripMemberDto dto : dtos) {
            result.add(fromMember(dto));
        }
        return result;
    }
}
