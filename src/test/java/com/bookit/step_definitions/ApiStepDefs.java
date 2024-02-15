package com.bookit.step_definitions;


import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookitUtils;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DB_Util;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApiStepDefs {

    Response response;
    JsonPath jsonPath;
    String accessToken;
    SelfPage selfPage = new SelfPage();

    @Given("I logged Bookit api as a {string}")
    public void i_logged_bookit_api_as_a(String role) {
        accessToken = BookitUtils.generateTokenByRole(role);
    }

    @When("I sent get request to {string} endpoint")
    public void i_sent_get_request_to_endpoint(String endPoint) {
        response = given()
                .accept(ContentType.JSON)
                .header("Authorization", accessToken)
                .when()
                .get(Environment.BASE_URL + endPoint)
                .then().extract().response();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(Integer expectedStatusCode) {
        assertThat(response.statusCode(), is(equalTo(expectedStatusCode)));
    }

    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        assertThat(response.getContentType(), is(equalTo(expectedContentType)));
    }

    @Then("role is {string}")
    public void role_is(String expectedRole) {
        jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("role"), is(equalTo(expectedRole)));
    }

    @Then("the information about current user from api and database should match")
    public void the_information_about_current_user_from_api_and_database_should_match() {
        Map<String, Object> actualCurrentUser = response.as(Map.class);
        int id = (int) (actualCurrentUser.get("id"));

        List<String> actualList = new ArrayList<>();

        for (Map.Entry<String, Object> each : actualCurrentUser.entrySet()) {// to adding all map value in the List
            actualList.add(each.getValue().toString());
        }
        DB_Util.runQuery("SELECT id,firstname,lastname,role FROM USERS WHERE users.id = " + id + "");
        List<String> expectedCurrentUser = DB_Util.getRowDataAsList(1);

        assertThat(actualList, equalTo(expectedCurrentUser));
    }

    @Then("UI,API and Database user information must be match")
    public void ui_api_and_database_user_information_must_be_match() {
        String uiName = selfPage.name.getText();
        String uiRole = selfPage.role.getText();
        Map<String, Object> apiCurrentUser = response.as(Map.class);
        int id = (int) (apiCurrentUser.get("id"));
        String apiName = apiCurrentUser.get("firstName") + " " + apiCurrentUser.get("lastName");
        String apiRole = apiCurrentUser.get("role").toString();

        DB_Util.runQuery("SELECT firstname,lastname,role FROM USERS WHERE users.id = " + id + "");
        List<String> dbCurrentUser = DB_Util.getRowDataAsList(1);

        String dbName = dbCurrentUser.get(0) + " " + dbCurrentUser.get(1);
        String dbRole = dbCurrentUser.get(2);

        assertThat(uiName, equalTo(apiName));
        assertThat(uiRole, equalTo(apiRole));

        assertThat(uiName, equalTo(dbName));
        assertThat(uiRole, equalTo(dbRole));

        System.out.println("UI Name--> "+uiName+" UI Role--> "+uiRole+" DB NAme--> "+ dbName+" DB Role-->  "+dbRole+" Api Role--> "+apiRole+" Api Name--> "+apiName);
    }
}
