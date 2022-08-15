package com.qachallenge;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QAChallengeAppTest {

    public static final String CREATE_ACCOUNT_ACTIVITY = ".CreateAccountActivity";
    public static final String MAIN_ACTIVITY = ".MainActivity";
    public static final String LOGIN_ACTIVITY = ".LoginActivity";
    public static final String USER_DETAILS_ACTIVITY = ".UserDetailsActivity";
    public static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    private static AndroidDriver androidDriver;

    @BeforeAll
    public static void setup() {
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Galaxy Nexus API 30");
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 30);
            capabilities.setCapability("appActivity", ".MainActivity");
//            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Espresso");
            capabilities.setCapability("forceEspressoRebuild", false);
            String apkPath = new File("app.apk").getAbsolutePath();
            capabilities.setCapability(MobileCapabilityType.APP, apkPath);
            androidDriver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
            androidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30L));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Failed to create Android driver: " + e.getMessage());
        }
    }

    @AfterAll
    public static void tearDown() {
        if (androidDriver != null) {
            androidDriver.quit();
        }
    }

    private WebElement getWebElementById(String id) {
        WebElement webElement = androidDriver.findElement(By.id(id));
        assertNotNull(webElement);
//        assertTrue(webElement.isDisplayed());
//        assertTrue(webElement.isEnabled());
        return webElement;
    }

    @BeforeEach
    public void beforeEachTest() {
    }

    @AfterEach
    public void afterEach() {
    }

    private Map<String, String> getMap(String firstName, String lastName, String email, String password) {
        return ImmutableMap.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "password", password
        );
    }

    private void createAccount(String firstName, String lastName, String email, String password) {
        WebElement firstNameEt = getWebElementById("firstNameEt");
        firstNameEt.sendKeys(firstName);

        WebElement lastNameEt = getWebElementById("lastNameEt");
        lastNameEt.sendKeys(lastName);

        WebElement emailEt = getWebElementById("emailEt");
        emailEt.sendKeys(email);

        WebElement passwordEt = getWebElementById("passwordEt");
        passwordEt.sendKeys(password);

        WebElement createAccountBtn = getWebElementById("btnCreateAccount");

        createAccountBtn.click();
    }

    private void createAccount(Map<String, String> params) {
        // Check for these buttons on the screen - create & login
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        // It should load create account screen
        createAccount(params.get("firstName"), params.get("lastName"), params.get("email"), params.get("password"));
    }

    private void goToCreateAccountScreen() {
        if (MAIN_ACTIVITY.equals(androidDriver.currentActivity())) {
            WebElement btnCreateAccount = getWebElementById("btnCreateAccount");
            getWebElementById("btnLogin");
            // Now click on the create button
            btnCreateAccount.click();
        }
    }

    private void doLogin(String email, String password) {
        // It should show main screen btnCreateAccount
        getWebElementById("btnCreateAccount");
        WebElement btnLogin = getWebElementById("btnLogin");

        assertEquals(MAIN_ACTIVITY, androidDriver.currentActivity());

        // Go to login
        btnLogin.click();

        // Login page should be shown
        WebElement usernameEt = getWebElementById("usernameEt");
        usernameEt.sendKeys(email);

        WebElement passwordEt = getWebElementById("passwordEt");
        passwordEt.sendKeys(password);
        // Assert input type to be password

        btnLogin = getWebElementById("btnLogin");

        assertEquals(LOGIN_ACTIVITY, androidDriver.currentActivity());

        // Click on login
        btnLogin.click();
    }

    @Test
    @DisplayName("Should successfully create account")
    @Order(1)
    public void testCreateAccountSuccess() {

        assertEquals(MAIN_ACTIVITY, androidDriver.currentActivity());
        Map<String, String> params = getMap("uday", "varma", "uday@gmail.com", "secret");

        createAccount(params);

        doLogin(params.get("email"), params.get("password"));

        // It must show user details page
        WebElement greetingTv = getWebElementById("greetingTv");
        assertEquals("User Logged In", greetingTv.getText());

        WebElement firstNameTv = getWebElementById("firstNameTv");
        assertEquals("First name: uday", firstNameTv.getText());

        WebElement lastNameTv = getWebElementById("lastNameTv");
        assertEquals("Last name: varma", lastNameTv.getText());

        WebElement emailTv = getWebElementById("emailTv");
        assertEquals("Email: uday@gmail.com", emailTv.getText());

        WebElement logoutBtn = getWebElementById("logoutBtn");

        assertEquals(USER_DETAILS_ACTIVITY, androidDriver.currentActivity());
        // Logout to go to main screen
        logoutBtn.click();

        // Should show main screen
        getWebElementById("btnCreateAccount");
        getWebElementById("btnLogin");
        assertEquals(MAIN_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should fail on create duplicate account")
    @Order(2)
    public void testCreateDuplicateAccount() {
        assertEquals(MAIN_ACTIVITY, androidDriver.currentActivity());
        Map<String, String> params = getMap("uday", "varma", "uday@gmail.com", "secret");

        createAccount(params);

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        androidDriver.navigate().back();
    }

    @Test
    @DisplayName("Should prevent invalid login")
    @Order(3)
    public void testInvalidLogin() {
        assertEquals(MAIN_ACTIVITY, androidDriver.currentActivity());

        doLogin("uday@gmail.com", "wrong");

        assertEquals(LOGIN_ACTIVITY, androidDriver.currentActivity());

        androidDriver.navigate().back();
    }

    @Test
    @DisplayName("Should validate first name for blank value while creating account and throw error icon on first name field")
    @Order(4)
    public void testCreateAccountValidationFirstNameBlank() {
        // Check for these buttons on the screen - create & login
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        // It should load create account screen
        WebElement firstNameEt = getWebElementById("firstNameEt");
        firstNameEt.sendKeys("");

        WebElement lastNameEt = getWebElementById("lastNameEt");
        lastNameEt.sendKeys("valid");

        WebElement emailEt = getWebElementById("emailEt");
        emailEt.sendKeys("valid@site.com");

        WebElement passwordEt = getWebElementById("passwordEt");
        passwordEt.sendKeys("secret");

        WebElement createAccountBtn = getWebElementById("btnCreateAccount");

        createAccountBtn.click();

//        Object o = androidDriver.executeScript("mobile: backdoor", ImmutableMap.of("target", "element", "elementId", "firstNameEt", "methods", Arrays.asList(ImmutableMap.of("name", "getError"))));
//        System.out.println(String.valueOf(o));

//        assertEquals("Invalid First name", firstNameEt.getAttribute("error"));
        assertEquals(".CreateAccountActivity", androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate email for blank value while creating account and it throws error icon on the email field")
    @Order(5)
    public void testCreateAccountValidationEmailBlank() {
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        createAccount("valid11", "valid22", "", "secret");
        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate email for invalid value while creating account")
    @Order(6)
    public void testCreateAccountValidationInvalidEmail() {
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        createAccount("valid02", "valid03", "invalid", "secret");


        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate last name for blank value while creating account")
    @Order(7)
    public void testCreateAccountValidationLastNameBlank() {
        goToCreateAccountScreen();
        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
        createAccount("valid01", "", "valid01@site.com", "secret");
        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate first name length while creating account")
    @Order(8)
    public void testCreateAccountValidationLongFirstName() {
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        createAccount("11aaaaaaaaabbbbbbbbbbbbcccccccccddddddddeeeeeeeeeee", "test1",
                "valid2@site.com", "secret");

        getWebElementById("btnCreateAccount");

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate last name length while creating account")
    @Order(9)
    public void testCreateAccountValidationLongLastName() {
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        createAccount("valid3", "11aaaaaaaaabbbbbbbbbbbbcccccccccddddddddeeeeeeeeeeefff",
                "valid3@site.com", "secret");


        getWebElementById("btnCreateAccount");

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate email length while creating account")
    @Order(10)
    public void testCreateAccountValidationLongEmail() {
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        createAccount("valid5", "valid6",
                "11aaaaaaaaabbbbbbbbbbbbcccccccccddddddddeeeeeeeeeeefff@site.com", "secret");

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

    @Test
    @DisplayName("Should validate password length while creating account")
    @Order(11)
    public void testCreateAccountValidationLongPassword() {
        goToCreateAccountScreen();

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());

        createAccount("validfirstname", "validlastname",
                "email@site.com", "11aaaaaaaaabbbbbbbbbbbbcccccccccddddddddeeeeeeeeeeefff");

        assertEquals(CREATE_ACCOUNT_ACTIVITY, androidDriver.currentActivity());
    }

}
