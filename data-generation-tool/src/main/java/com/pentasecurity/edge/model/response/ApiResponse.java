package com.pentasecurity.edge.model.response;

import com.pentasecurity.edge.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiResponse extends BaseModel {
    int code;
    String message;    
}