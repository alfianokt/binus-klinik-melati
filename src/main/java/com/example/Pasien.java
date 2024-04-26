package com.example;

public class Pasien {
  private Integer id;
  private String name;
  private Long nik;
  private String date;
  private String address;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getNik() {
    return nik;
  }

  public void setNik(Long nik) {
    this.nik = nik;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}