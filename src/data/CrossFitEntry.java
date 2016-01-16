package data;

/**
 * CrossFit Affiliate Data Extractor
 *
 * @package	data
 * @author	Jay <imjching@hotmail.com>
 * @copyright	Copyright (C) 2015, Jay <imjching@hotmail.com>
 * @license	Modified BSD License (refer to LICENSE)
 */
public class CrossFitEntry {

    private int id;
    private String name, address, phone, url, longitude, latitude;

    public CrossFitEntry(int id, String latitude, String longitude) { // 34.0423563,-118.4413598 (lattitude, longitude)
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        //System.out.println(latitude + ", " + longitude);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String[] toCSVString() {
        return new String[]{String.valueOf(id), name, address, phone, url, latitude, longitude};
    }
}
//The data that needs to be extracted:
//1. Name of Business (Crossfit Box)
//2. Full Address (street, city, state, zip) specific to country
//3. Phone Number
//4. Website URL
//5. Longitude & Latitude
//Sample:
//CrossFit 644
//Calle 5a #6-44
//Cartagena, Bolivar
//(310) 637-1102
