package com.buggysoft.verification.entity;


import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("verification_codes")
public class Verification {
  @TableId
  private String email;
  private String code;
  private LocalDateTime createdAt;
}
