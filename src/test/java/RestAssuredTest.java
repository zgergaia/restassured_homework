import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.*;

import static io.restassured.RestAssured.given;

public class RestAssuredTest {
    private Object isbn = null;
    private String token = null;

    public Response _extractResponse(Response spec) {
        return spec.then().extract().response();
    }

    public JsonPath getJson(Response res) {
        return res.getBody().jsonPath();
    }

    public Response sendGet(String url) {
        return this._extractResponse(given()
                .contentType(ContentType.JSON)
                .when()
                .get(url));
    }

    public Response sendPost(String url, Object payload, Boolean delete) {
        RequestSpecification spec = given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + this.token)
                .body(payload)
                .when();

        Response res = spec.post(url);;

        if (delete)
            res = spec.delete(url);

        return this._extractResponse(res);
    }

    @Test
    public void test() throws JsonProcessingException {
        JSONObject payload = new JSONObject();
        payload.put("userName", "dwight");
        payload.put("password", "DwightSchrute&5");

        this.token = this.getJson(sendPost("https://demoqa.com/Account/v1/GenerateToken", payload.toJSONString(), false)).getString("token");

        var response = this.sendGet("https://demoqa.com/BookStore/v1/Books");
        Assert.assertEquals(response.statusCode(), 200);
        var map = this.getJson(response).getMap("books[0]");
        this.isbn = map.get("isbn");

        Assert.assertEquals(map.get("publisher"), "O'Reilly Media");
        Assert.assertEquals(map.get("pages"), 234);

        JSONArray isbns = new JSONArray();
        var isbn = new JSONObject();
        isbn.put("isbn", this.isbn);
        isbns.add(isbn);

        payload = new JSONObject();
        payload.put("userId", "eda25c97-5f46-4df6-9186-05a9232fbb72");
        payload.put("collectionOfIsbns", isbns);

        response = sendPost("https://demoqa.com/BookStore/v1/Books", payload.toJSONString(), false);
        Assert.assertEquals(response.statusCode(), 201);

        Books books = response.as(Books.class);
        System.out.println(books);

    }

    @AfterTest
    public void destruct() {
        var payload = new JSONObject();
        payload.put("userId", "eda25c97-5f46-4df6-9186-05a9232fbb72");
        payload.put("isbn", this.isbn);

        var response = sendPost("https://demoqa.com/BookStore/v1/Book", payload.toJSONString(), true);
        Assert.assertEquals(response.statusCode(), 204);
    }
}