package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.weather.WeatherDto;
import com.example.mybaghackathon.data.remote.dto.weather.WeatherFeedbackDto;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;

import java.util.ArrayList;
import java.util.List;

// WeatherDto를 Weather 모델로 변환
public final class WeatherMapper {

    private WeatherMapper() {
    }

    // 일별 예보 목록을 변환한다
    public static List<Weather> from(List<WeatherDto> dtos) {
        List<Weather> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (WeatherDto dto : dtos) {
            result.add(new Weather(
                    dto.getDate(),
                    dto.getTempMax(),
                    dto.getTempMin(),
                    dto.getCondition(),
                    dto.getPrecipitationProbability()
            ));
        }
        return result;
    }

    // 날씨 기반 옷차림/음식/숙소 GPT 조언을 변환한다
    public static WeatherFeedback from(WeatherFeedbackDto dto) {
        return new WeatherFeedback(dto.getClothing(), dto.getFood(), dto.getAccommodationNotes());
    }

    // 서버 condition 문자열을 WeatherIconView(A9)의 타입 정수로 변환: 0=맑음 1=비 2=흐림 3=눈
    public static int toIconType(String condition) {
        if (condition == null) {
            return 0;
        }
        switch (condition) {
            case "rain":
                return 1;
            case "cloud":
                return 2;
            case "snow":
                return 3;
            default:
                return 0;
        }
    }
}
