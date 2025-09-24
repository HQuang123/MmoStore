package com.swp.mmostore.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ActivityLog")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Action", length = 255)
    private String action;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @Column(name = "CreateAt", updatable = false)
    private LocalDateTime createAt;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt;

    // Relations
    @Column(name = "CreateBy")
    private int createBy;

    @Column(name = "UpdateBy")
    private int updateBy;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;
}
