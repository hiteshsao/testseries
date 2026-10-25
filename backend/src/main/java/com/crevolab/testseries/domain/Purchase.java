package com.crevolab.testseries.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestSeries test;

    @Column(name = "student_id", nullable = false, length = 100)
    private String studentId;

    // DEMO-ONLY: provider is simulated for demo test series purchase transactions
    @Column(nullable = false, length = 50)
    private String provider = "SIMULATED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Purchase() {
    }

    public Purchase(TestSeries test, String studentId, String provider) {
        this.test = test;
        this.studentId = studentId;
        this.provider = provider != null ? provider : "SIMULATED";
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestSeries getTest() {
        return test;
    }

    public void setTest(TestSeries test) {
        this.test = test;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
