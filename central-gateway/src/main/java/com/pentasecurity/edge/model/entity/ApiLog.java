package com.pentasecurity.edge.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.pentasecurity.edge.model.BaseModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiLog extends BaseModel {
    @Id
    @Column(name="log_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    int logId = 0;
    @Column(length = 20)
    String remoteAddr;
    @Column(length = 20)
    String device;
    @Column(length = 10)
    String method;
    @Column(length = 200)
    String url;
    @Column(columnDefinition = "TEXT")
    String requestParameters;
    @Column(columnDefinition = "TEXT")
    String requestBody;
    @Column(columnDefinition = "TEXT")
    String responseBody;
    @Column(columnDefinition = "DATETIME")
    Date createdAt;
}