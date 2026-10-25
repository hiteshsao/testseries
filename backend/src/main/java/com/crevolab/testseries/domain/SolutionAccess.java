package com.crevolab.testseries.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "solution_access")
public class SolutionAccess {

    @EmbeddedId
    private SolutionAccessId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("testId")
    @JoinColumn(name = "test_id", nullable = false)
    private TestSeries test;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    public SolutionAccess() {
    }

    public SolutionAccess(TestSeries test, String studentId, Integer viewCount, Integer downloadCount) {
        this.test = test;
        this.id = new SolutionAccessId(test.getId(), studentId);
        this.viewCount = viewCount != null ? viewCount : 0;
        this.downloadCount = downloadCount != null ? downloadCount : 0;
    }

    public SolutionAccessId getId() {
        return id;
    }

    public void setId(SolutionAccessId id) {
        this.id = id;
    }

    public TestSeries getTest() {
        return test;
    }

    public void setTest(TestSeries test) {
        this.test = test;
        if (test != null && this.id != null) {
            this.id.setTestId(test.getId());
        }
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
}
