package pt.unl.fct.di.syspriv.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 50)
    private String postalCode;

    @Column(nullable = false, length = 50)
    private String cardNumber;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "retention_until")
    private LocalDateTime retentionUntil;

    @Column(name = "erasure_requested")
    private Boolean erasureRequested = false;

    @Column(name = "erasure_completed_at")
    private LocalDateTime erasureCompletedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_subject_id")
    private List<ConsentRecord> consentRecords;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_subject_id")
    private List<GDPRRequest> gdprRequests;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id")
    private Employee primaryDoctor;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    public Patient() {}

    public Patient(String name, LocalDate birthDate, String address, String postalCode, String cardNumber, Employee primaryDoctor) {
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
        this.postalCode = postalCode;
        this.cardNumber = cardNumber;
        this.primaryDoctor = primaryDoctor;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getStringId() { return Long.toString(id); }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getRetentionUntil() { return retentionUntil; }
    public void setRetentionUntil(LocalDateTime retentionUntil) { this.retentionUntil = retentionUntil; }

    public Boolean getErasureRequested() { return erasureRequested; }
    public void setErasureRequested(Boolean erasureRequested) { this.erasureRequested = erasureRequested; }
    public LocalDateTime getErasureCompletedAt() { return erasureCompletedAt; }
    public void setErasureCompletedAt(LocalDateTime erasureCompletedAt) { this.erasureCompletedAt = erasureCompletedAt; }

    public List<ConsentRecord> getConsentRecords() { return consentRecords; }
    public void setConsentRecords(List<ConsentRecord> consentRecords) { this.consentRecords = consentRecords; }
    public List<GDPRRequest> getGdprRequests() { return gdprRequests; }
    public void setGdprRequests(List<GDPRRequest> gdprRequests) { this.gdprRequests = gdprRequests; }

    public Employee getPrimaryDoctor() { return primaryDoctor; }
    public void setPrimaryDoctor(Employee primaryDoctor) { this.primaryDoctor = primaryDoctor; }
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}