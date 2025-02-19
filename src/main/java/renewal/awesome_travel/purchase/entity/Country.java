package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Country")
public class Country {

    @Id
    @Column(nullable = false, unique = true)
    private String countryCode; //국가코드 kor

    @Column(nullable = false)
    private String countryName; //영문 국가명 Republic of Korea

    @Column(nullable = true)
    private String countryNameLocal; //현지국가명 대한민국

    public Country(String countryCode, String countryName, String countryNameLocal) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.countryNameLocal = countryNameLocal;
    }

    public void updateCountry(String countryName, String countryNameLocal) {
        if (countryName != null) {
            this.countryName = countryName;
        }
        if (countryNameLocal != null) {
            this.countryNameLocal = countryNameLocal;
        }
    }
}
