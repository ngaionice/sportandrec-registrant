package scheduler;

import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.beans.property.StringProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    void initialize() {
        WebDriverManager.chromedriver().setup();
        if (headless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--window-size=1920,1080");
            this.driver = new ChromeDriver(options);
        } else {
            this.driver = new ChromeDriver();
        }
        this.wait = new WebDriverWait(driver, 10);
        driver.get(url);
    }

    void login() {

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

    void registerEvent() {

        // accept cookies
        wait.until(ExpectedConditions.elementToBeClickable(By.id("gdpr-cookie-accept"))).click();

        // identify the card with the target date and time
        WebElement optionsParent = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("list-group")));
        List<WebElement> options = optionsParent.findElements(By.className("program-schedule-card"));
        for (WebElement option : options) {
            WebElement dateElement = option.findElement(By.className("program-schedule-card-header"));
            String date = dateElement.getText();
            if (date.contains(this.date.format(dateFormatter))) {
                WebElement timeElement = option.findElement(By.tagName("small"));
                String time = timeElement.getText();
                if (time.split(" - ")[0].contains(this.time.format(timeFormatter))) {
                    // TODO: try/catch with custom error; if we get here then we found the card, but might have signed up already/full
                    option.findElement(By.tagName("button")).click();
                    break;
                }
            }
        }

        // accept the waiver
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnAccept"))).click();

        // add to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"mainContent\"]/div[2]/form[2]/div[4]/button[2]"))).click();

        // check out
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkoutButton"))).click();
//        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"CheckoutModal\"]/div/div[2]/button[2]"))).click();
    }

    void end() {
        driver.close();
    }
}
