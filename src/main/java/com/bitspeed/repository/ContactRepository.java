package com.bitspeed.repository;

//import com.bitspeed.identity_reconciliation.model.Contact;
import com.bitspeed.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    // Finds any contact that matches either the email or phone number
    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);

    // Helps find all contacts belonging to the same linked group
    List<Contact> findByLinkedIdOrId(Integer linkedId, Integer id);
}