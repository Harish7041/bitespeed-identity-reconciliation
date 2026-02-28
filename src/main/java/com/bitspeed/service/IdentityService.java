package com.bitspeed.service;

import com.bitspeed.dto.IdentityRequest;
import com.bitspeed.dto.IdentityResponse;
import com.bitspeed.model.Contact;
import com.bitspeed.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdentityService {

    @Autowired
    private ContactRepository repository;

    @Transactional
    public IdentityResponse reconcile(IdentityRequest request) {
        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        List<Contact> matchedContacts = repository.findByEmailOrPhoneNumber(email, phone);

        if (matchedContacts.isEmpty()) {
            Contact newPrimary = createContact(email, phone, null, Contact.LinkPrecedence.primary);
            return buildResponse(newPrimary);
        }

        Contact primaryContact = findOldestPrimary(matchedContacts);
        handleMergers(matchedContacts, primaryContact);

        boolean isNewEmail = email != null && matchedContacts.stream().noneMatch(c -> email.equals(c.getEmail()));
        boolean isNewPhone = phone != null && matchedContacts.stream().noneMatch(c -> phone.equals(c.getPhoneNumber()));

        if (isNewEmail || isNewPhone) {
            createContact(email, phone, primaryContact.getId(), Contact.LinkPrecedence.secondary);
        }

        return buildResponse(primaryContact);
    }

    private Contact findOldestPrimary(List<Contact> matches) {
        return matches.stream()
                .map(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.primary ? c :
                        repository.findById(c.getLinkedId()).orElse(c))
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElse(matches.get(0));
    }

    private void handleMergers(List<Contact> matches, Contact truePrimary) {
        for (Contact contact : matches) {
            if (contact.getLinkPrecedence() == Contact.LinkPrecedence.primary && !contact.getId().equals(truePrimary.getId())) {
                contact.setLinkPrecedence(Contact.LinkPrecedence.secondary);
                contact.setLinkedId(truePrimary.getId());
                contact.setUpdatedAt(LocalDateTime.now());
                repository.save(contact);
            }
        }
    }

    private Contact createContact(String email, String phone, Integer linkedId, Contact.LinkPrecedence precedence) {
        Contact contact = new Contact();
        contact.setEmail(email);
        contact.setPhoneNumber(phone);
        contact.setLinkedId(linkedId);
        contact.setLinkPrecedence(precedence);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return repository.save(contact);
    }

    private IdentityResponse buildResponse(Contact primary) {
        // Collect all related contacts to build the consolidated view
        List<Contact> allLinked = repository.findByLinkedIdOrId(primary.getId(), primary.getId());

        Set<String> emails = new LinkedHashSet<>();
        if (primary.getEmail() != null) emails.add(primary.getEmail());
        allLinked.forEach(c -> { if(c.getEmail() != null) emails.add(c.getEmail()); });

        Set<String> phones = new LinkedHashSet<>();
        if (primary.getPhoneNumber() != null) phones.add(primary.getPhoneNumber());
        allLinked.forEach(c -> { if(c.getPhoneNumber() != null) phones.add(c.getPhoneNumber()); });

        // secondaryIds list must match the DTO's Integer type
        List<Integer> secondaryIds = allLinked.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.secondary)
                .map(Contact::getId)
                .collect(Collectors.toList());

        return IdentityResponse.builder()
                .contact(IdentityResponse.ContactDto.builder()
                        .primaryContatctId(primary.getId()) // Matches misspelled DTO
                        .emails(new ArrayList<>(emails))
                        .phoneNumbers(new ArrayList<>(phones))
                        .secondaryContactIds(secondaryIds) // This clears the red mark
                        .build())
                .build();
    }
}