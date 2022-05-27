package nextstep.subway.station;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.BaseAcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends BaseAcceptanceTest {

    private static final String RESOURCE = "/stations";

    private ExtractableResponse<Response> 지하철역_등록(String name) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);

        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(RESOURCE)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철역_조회() {
        return RestAssured.given().log().all()
                .when().get(RESOURCE)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철역_삭제(Long stationId) {
        return RestAssured.given().log().all()
                .accept(ContentType.JSON)
                .when().delete(RESOURCE + "/{id}", stationId)
                .then().log().all()
                .extract();
    }

    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void 지하철역_등록() {
        // when
        String station = "강남역";
        ExtractableResponse<Response> response = 지하철역_등록(station);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> stationNames = 지하철역_조회().jsonPath().getList("name", String.class);
        assertThat(stationNames).containsAnyOf("강남역");
    }

    /**
     * Given 지하철역을 생성하고
     * When 기존에 존재하는 지하철역 이름으로 지하철역을 생성하면
     * Then 지하철역 생성이 안된다
     */
    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
    @Test
    void createStationWithDuplicateName() {
        // given
        String station = "강남역";
        지하철역_등록(station);

        // when
        ExtractableResponse<Response> response = 지하철역_등록(station);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("지하철역을 조회한다.")
    @Test
    void getStations() {
        // given
        지하철역_등록("강남역");
        지하철역_등록("역삼역");

        // when
        List<String> stationNames = 지하철역_조회().jsonPath().getList("name", String.class);

        // then
        assertThat(stationNames).hasSize(2);
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStations() {
        // given
        Long stationId = 지하철역_등록("강남역").jsonPath().getLong("id");

        // when
        지하철역_삭제(stationId);

        // then
        List<String> stationNames = 지하철역_조회().jsonPath().getList("name", String.class);
        assertThat(stationNames).hasSize(0);
    }
}
