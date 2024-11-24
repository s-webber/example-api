package com.example.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "patient")
public class PatientEntity {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "patient_id")
   private int id;

   @Column(name = "nhs_number", unique = true, nullable = false)
   private String nhsNumber;

   @Column(name = "name", nullable = false)
   private String name;

   @Column(name = "date_of_birth", nullable = false)
   private LocalDate dateOfBirth;

   @Column(name = "address")
   private String address;

   @Column(name = "postcode")
   private String postcode;

   @Column(name = "telephone_number")
   private String telephoneNumber;

   @Column(name = "email_address")
   private String emailAddress;

   @Column(name = "updated_by", nullable = false)
   private int updatedBy;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getNhsNumber() {
      return nhsNumber;
   }

   public void setNhsNumber(String nhsNumber) {
      this.nhsNumber = nhsNumber;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public LocalDate getDateOfBirth() {
      return dateOfBirth;
   }

   public void setDateOfBirth(LocalDate dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
   }

   public String getAddress() {
      return address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public String getPostcode() {
      return postcode;
   }

   public void setPostcode(String postcode) {
      this.postcode = postcode;
   }

   public String getTelephoneNumber() {
      return telephoneNumber;
   }

   public void setTelephoneNumber(String telephoneNumber) {
      this.telephoneNumber = telephoneNumber;
   }

   public String getEmailAddress() {
      return emailAddress;
   }

   public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
   }

   public int getUpdatedBy() {
      return updatedBy;
   }

   public void setUpdatedBy(int updatedBy) {
      this.updatedBy = updatedBy;
   }
}