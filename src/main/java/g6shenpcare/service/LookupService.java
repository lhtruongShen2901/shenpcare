package g6shenpcare.service;


import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Order;
import g6shenpcare.entity.Pets;
import g6shenpcare.models.dto.BookingDetailDTO;
import g6shenpcare.models.dto.LookupResultDTO;
import g6shenpcare.models.dto.OrderDetailDTO;
import g6shenpcare.models.dto.TicketDetailDTO;
import g6shenpcare.models.entity.Ticket;
import g6shenpcare.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class LookupService {

    private final CustomerProfileRepository customerRepository;
    private final PetRepository petRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
//    private final PetMedicalRecordRepository medicalRecordRepository;


    public LookupResultDTO search(String searchType, String keyword) {
        LookupResultDTO result = new LookupResultDTO();

        switch (searchType.toLowerCase()) {
            case "customer":
                searchByCustomer(keyword, result);
                break;
            case "pet":
                searchByPet(keyword, result);
                break;
            case "booking":
                searchByBooking(keyword, result);
                break;
            case "order":
                searchByOrder(keyword, result);
                break;
            case "ticket":
                searchByTicket(keyword, result);
                break;
            default:
                throw new IllegalArgumentException(STR."Loại tìm kiếm không hợp lệ: \{searchType}");
        }

        return result;
    }

    private void searchByCustomer(String keyword, LookupResultDTO result) {
        List<CustomerProfile> customers;

        if (keyword.contains("@")) {
            // Search by email
            customers = customerRepository.findByEmailContainingIgnoreCase(keyword);
        } else {
            // Search by phone
            customers = customerRepository.findByPhoneContaining(keyword);
        }

        if (!customers.isEmpty()) {
            CustomerProfile customer = customers.get(0);
            result.setCustomer(customer);
            loadCustomerRelatedData(customer.getCustomerId(), result);
        }
    }


    private void searchByPet(String keyword, LookupResultDTO result) {
        List<Pets> pets;

        Pets pet = petRepository.findByPetCode(keyword).orElse(null);

        if (pet == null) {
            pets = petRepository.findByNameContainingIgnoreCase(keyword);
            if (!pets.isEmpty()) {
                pet = pets.get(0);
            }
        }

        if (pet != null) {
            result.setPets(List.of(pet));
            CustomerProfile customer = customerRepository.findById(pet.getCustomerId()).orElse(null);
            result.setCustomer(customer);

            if (customer != null) {
                loadCustomerRelatedData(customer.getCustomerId(), result);
            }
        }
    }


    private void searchByBooking(String keyword, LookupResultDTO result) {
        try {
            Integer bookingId = Integer.parseInt(keyword);
            Booking booking = bookingRepository.findById(bookingId).orElse(null);

            if (booking != null) {
                result.setBookings(List.of(booking));
                CustomerProfile customer = customerRepository.findById(booking.getCustomerId()).orElse(null);
                result.setCustomer(customer);

                Pets pet = petRepository.findById(booking.getPetId()).orElse(null);
                if (pet != null) {
                    result.setPets(List.of(pet));
                }

                if (customer != null) {
                    loadCustomerRelatedData(customer.getCustomerId(), result);
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Booking ID không hợp lệ");
        }
    }


    private void searchByOrder(String keyword, LookupResultDTO result) {
        try {
            Integer orderId = Integer.parseInt(keyword);
            Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);

            if (order != null) {
                result.setOrders(List.of(order));
                CustomerProfile customer = customerRepository.findById(order.getCustomerId()).orElse(null);
                result.setCustomer(customer);

                if (customer != null) {
                    loadCustomerRelatedData(customer.getCustomerId(), result);
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Order ID không hợp lệ");
        }
    }


    private void searchByTicket(String keyword, LookupResultDTO result) {
        try {
            Integer ticketId = Integer.parseInt(keyword);
            Ticket ticket = ticketRepository.findById(Long.valueOf(ticketId)).orElse(null);

            if (ticket != null) {
                result.setSupportTickets(List.of(ticket));
                CustomerProfile customer = customerRepository.findById(ticket.getCustomer().getUserId()).orElse(null);
                result.setCustomer(customer);

                if (customer != null) {
                    loadCustomerRelatedData(customer.getCustomerId(), result);
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ticket ID không hợp lệ");
        }
    }


    private void loadCustomerRelatedData(Integer customerId, LookupResultDTO result) {
        // Load pets
        List<Pets> pets = petRepository.findByCustomerId(customerId);
        result.setPets(pets);

        // Load medical records for all pets
//        List<PetMedicalRecord> allRecords = new ArrayList<>();
//        for (Pets pet : pets) {
//            List<PetMedicalRecord> records = medicalRecordRepository.findByPetIdOrderByVisitDateDesc(pet.getPetId());
//            allRecords.addAll(records);
//        }
//        result.setMedicalRecords(allRecords);

        // Load bookings
        List<Booking> bookings = bookingRepository.findByCustomerIdOrderByStartTimeDesc(customerId);
        result.setBookings(bookings);

        // Load orders
        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
        result.setOrders(orders);

        // Load support tickets
        List<Ticket> tickets = ticketRepository.findByCustomer_UserIdOrderByCreatedAtDesc(customerId);
        result.setSupportTickets(tickets);
    }


//    public PetMedicalHistoryDTO getPetMedicalHistory(Integer petId) {
//        Pets pet = petRepository.findById(petId)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thú cưng"));
//
//        CustomerProfile customer = customerRepository.findById(pet.getCustomerId())
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ sở hữu"));
//
//        List<PetMedicalRecord> records = medicalRecordRepository.findByPetIdOrderByVisitDateDesc(petId);
//
//        PetMedicalHistoryDTO dto = new PetMedicalHistoryDTO();
//        dto.setPet(pet);
//        dto.setCustomer(customer);
//        dto.setMedicalRecords(records);
//
//        return dto;
//    }


    public BookingDetailDTO getBookingDetail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking"));

        CustomerProfile customer = customerRepository.findById(booking.getCustomerId()).orElse(null);
        Pets pet = petRepository.findById(booking.getPetId()).orElse(null);

        // Try to get medical record if exists
//        PetMedicalRecord medicalRecord = medicalRecordRepository.findByBookingId(bookingId).orElse(null);

        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setBooking(booking);
        dto.setCustomer(customer);
        dto.setPet(pet);
//        dto.setMedicalRecord(medicalRecord);

        return dto;
    }


    public OrderDetailDTO getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        CustomerProfile customer = customerRepository.findById(order.getCustomerId()).orElse(null);

        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrder(order);
        dto.setCustomer(customer);
//        dto.setOrderItems(order.getItems());

        return dto;
    }


    public TicketDetailDTO getTicketDetail(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        CustomerProfile customer = customerRepository.findById(ticket.getCustomer().getUserId()).orElse(null);

        TicketDetailDTO dto = new TicketDetailDTO();
        dto.setTicket(ticket);
        dto.setCustomer(customer);
//        dto.setMessages(ticket.getMessages());

        return dto;
    }



}
