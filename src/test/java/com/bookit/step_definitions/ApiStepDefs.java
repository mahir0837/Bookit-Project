package com.bookit.step_definitions;


import com.bookit.utilities.BookitUtils;
import com.bookit.utilities.ConfigurationReader;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApiStepDefs {

    Response response;
    String accessToken;

    @Given("I logged Bookit api as a {string}")
    public void i_logged_bookit_api_as_a(String role) {
       accessToken= BookitUtils.generateTokenByRole(role);
    }

    @When("I sent get request to {string} endpoint")
    public void i_sent_get_request_to_endpoint(String endPoint) {
        response = given()
                .accept(ContentType.JSON)
                .header("Authorization",accessToken)
                .when()
                .get(ConfigurationReader.getProperty("base_url")+endPoint)
                .then().extract().response();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(Integer expectedStatusCode) {
        assertThat(response.statusCode(),is(equalTo(expectedStatusCode)));
    }

    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        assertThat(response.getContentType(),is(equalTo(expectedContentType)));
    }

    @Then("role is {string}")
    public void role_is(String expectedRole) {
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("role"),is(equalTo(expectedRole)));
    }

}
