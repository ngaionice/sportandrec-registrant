package scheduler;

import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.beans.property.StringProperty;
import model.DataModel;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Registrant {

    private WebDriver driver;
    private WebDriverWait wait;
    private final StringProperty userId;
    private final StringProperty password;
    private final String url;
    private final LocalDate date;
    private final LocalTime time;
    private final boolean headless;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("LLLL d, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    Registrant(StringProperty userId, StringProperty password, String url, LocalDate date, LocalTime time, boolean headless) {
        this.userId = userId;
        this.password = password;
        this.url = url;
        this.date = date;
        this.time = time;
        this.headless = headless;
    }

    void initialize() throws SignupException {
        WebDriverManager.chromedriver().setup();
        if (headless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--window-size=1920,1080");
            this.driver = new ChromeDriver(options);
        } else {
            this.driver = new ChromeDriver();
        }
        this.wait = new WebDriverWait(driver, 10);
        if (url == null) throw new SignupException("Event URL was empty, failed to sign up.");
        driver.get(url);
    }

    void login() throws SignupException {

        if (userId.get() == null || password.get() == null) throw new SignupException("UTORid and/or password was not set, failed to sign up.");

        // go from event page to login page
        WebElement loginLink = driver.findElement(By.id("loginLink"));
        loginLink.click();

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"divLoginOptions\"]/div[2]/div[2]/div/button")));
        loginBtn.click();

        // send id and password, then press login
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")))
                .sendKeys(userId.get());
        driver.findElement(By.id("password"))
                .sendKeys(password.get());
        driver.findElement(By.xpath("/html/body/div/div/div[1]/div[2]/form/button"))
                .click();
    }

    void registerEvent() throws SignupException {
        try {
            // accept cookies
            wait.until(ExpectedConditions.elementToBeClickable(By.id("gdpr-cookie-accept"))).click();

            // identify the card with the target date and time
            WebElement optionsParent = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("list-group")));
            List<WebElement> options = optionsParent.findElements(By.className("program-schedule-card"));

            int continueFlag = 1; // 1 = did not find card, 0 = successful signup, 2 = found card but could not sign up
            for (WebElement option : options) {
                WebElement dateElement = option.findElement(By.className("program-schedule-card-header"));
                String date = dateElement.getText();
                if (date.contains(this.date.format(dateFormatter))) {
                    WebElement timeElement = option.findElement(By.tagName("small"));
                    String time = timeElement.getText();
                    if (time.split(" - ")[0].contains(this.time.format(timeFormatter))) {
                        List<WebElement> registerBtn = option.findElements(By.tagName("button"));
                        if (registerBtn.size() == 1) {
                            registerBtn.get(0).click();
                            continueFlag = 0;
                        } else {
                            continueFlag = 2;
                        }
                        break;
                    }
                }
            }

            if (continueFlag == 2) {
                // send message saying could not sign up, might be full/signed up alrdy
                throw new SignupException("Could not sign up for event on " + date + " at " + time + ". It might be full, or you might've signed up already.");
            } else if (continueFlag == 1) {
                // could not find card, might be some unknown error.
                throw new SignupException("Event on " + date + " at " + time + " could not be found.");
            }

            // accept the waiver
            wait.until(ExpectedConditions.elementToBeClickable(By.id("btnAccept"))).click();

            // add to cart
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"mainContent\"]/div[2]/form[2]/div[4]/button[2]"))).click();

            // check out
            wait.until(ExpectedConditions.elementToBeClickable(By.id("checkoutButton"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"CheckoutModal\"]/div/div[2]/button[2]"))).click();
            // send message saying signup successful
            DataModel.setLatestMessage("Successfully signed up for event on " + date + " at " + time + ".");
        } catch (NoSuchElementException | TimeoutException e) {
            throw new SignupException("An unknown error occurred while signing up for event on " + date + " at " + time + ".");
        }
    }

    void end() {
        driver.close();
    }

}
