package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "Notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "Status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Unread'")
    private String status = "Unread";

    @Column(name = "CreateAt", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "UpdateAt", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "CreateBy")
    private Integer creatBy;

    @Column(name = "DeleteBy")
    private Integer deleteBy;

    @Column(name = "IsDeleted", insertable = false)
    private Boolean isDeleted;
}
