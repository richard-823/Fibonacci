import org.junit.*;
import org.junit.runners.MethodSorters;
import play.mvc.Http;

import java.net.URL;
import java.net.URLConnection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FibonacciTest {

    private static String endpoint = "/api/1/fibonacci/numbers";

    @BeforeClass
    public static void setUpGlobal() throws Exception {
    }

    @AfterClass
    public static void tearDownGlobal() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // precondition check, if fail, skip the test
        Assume.assumeTrue("Server " + getServerAddress() + " is not alive", isServerAlive(getServerAddress()));
    }

    @After
    public void tearDown() throws Exception {
    }

    private static boolean isServerAlive(String server) {
        if (server == null || server.isEmpty()) {
            return false;
        }

        try {
            URL serverUrl = new URL(server);
            URLConnection serverCon = serverUrl.openConnection();
            serverCon.connect();
            // add more checks...
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String getServerAddress() {
        // TODO：do not hardcode server host & port
        return "http://localhost:9000";
    }

    @Test
    public void testGetFibonacciNumbersWithListSuccess() {
        given().when().get(getServerAddress() + endpoint + "?amount=5").
                then().assertThat().statusCode(Http.Status.OK).body("size()", equalTo(5)).
                body("$", contains(0, 1, 1, 2, 3));
    }

    @Test
    public void testGetFibonacciNumbersWithListZeroAmount() {
        given().when().get(getServerAddress() + endpoint + "?amount=0").
                then().assertThat().statusCode(Http.Status.OK).body("size()", equalTo(0));
    }

    @Test
    public void testGetFibonacciNumbersWithListNegativeAmount() {
        given().when().get(getServerAddress() + endpoint + "?amount=-1").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithListOverflowAmount() {
        given().when().get(getServerAddress() + endpoint + "?amount=94").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithListAmountNotNumber() {
        given().when().get(getServerAddress() + endpoint + "?numbers?amount=foo").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationSuccess() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=10&limit=4").
                then().assertThat().statusCode(Http.Status.OK).body("amount", equalTo(20)).
                body("numbers.size", equalTo(4)).body("numbers", contains(55, 89, 144, 233)).
                body("previous", equalTo(getServerAddress() + endpoint + "?amount=20&offset=6&limit=4")).
                body("next", equalTo(getServerAddress() + endpoint + "?amount=20&offset=14&limit=4"));
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationNoPrev() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=0&limit=4").
                then().assertThat().statusCode(Http.Status.OK).body("amount", equalTo(20)).
                body("numbers.size", equalTo(4)).body("numbers", contains(0, 1, 1, 2)).
                body("previous", equalTo(null)).
                body("next", equalTo(getServerAddress() + endpoint + "?amount=20&offset=4&limit=4"));
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationNoNext() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=16&limit=4").
                then().assertThat().statusCode(Http.Status.OK).body("amount", equalTo(20)).
                body("numbers.size", equalTo(4)).body("numbers", contains(987, 1597, 2584, 4181)).
                body("previous", equalTo(getServerAddress() + endpoint + "?amount=20&offset=12&limit=4")).
                body("next", equalTo(null));
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationAlignToStart() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=3&limit=5").
                then().assertThat().statusCode(Http.Status.OK).body("amount", equalTo(20)).
                body("numbers.size", equalTo(5)).body("numbers", contains(2, 3, 5, 8, 13)).
                body("previous", equalTo(getServerAddress() + endpoint + "?amount=20&offset=0&limit=5")).
                body("next", equalTo(getServerAddress() + endpoint + "?amount=20&offset=8&limit=5"));
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationPartialPage() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=17&limit=5").
                then().assertThat().statusCode(Http.Status.OK).body("amount", equalTo(20)).
                body("numbers.size", equalTo(3)).body("numbers", contains(1597, 2584, 4181)).
                body("previous", equalTo(getServerAddress() + endpoint + "?amount=20&offset=12&limit=5")).
                body("next", equalTo(null));
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationOverflowAmount() {
        given().when().get(getServerAddress() + endpoint + "?amount=94&offset=10&limit=4").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationNegativeOffset() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=-1&limit=4").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationOverflowOffset() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=20&limit=4").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationNegativeLimit() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=10&limit=-1").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationZeroLimit() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=10&limit=0").
                then().assertThat().statusCode(Http.Status.OK).body("amount", equalTo(20)).
                body("numbers.size", equalTo(0)).
                body("previous", equalTo(null)).
                body("next", equalTo(null));
    }

    @Test
    public void testGetFibonacciNumbersWithPaginationOverflowLimit() {
        given().when().get(getServerAddress() + endpoint + "?amount=20&offset=10&limit=21").
                then().assertThat().statusCode(Http.Status.BAD_REQUEST);
    }
}
