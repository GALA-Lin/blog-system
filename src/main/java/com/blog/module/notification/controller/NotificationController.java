package com.blog.module.notification.controller;

import com.blog.DTO.notification.*;
import com.blog.VO.notification.*;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.module.notification.service.INotificationService;
import com.blog.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: GALA_Lin
 * @Date: 2025-10-22-12:08
 * @Description:
 */
@Tag(name = "通知管理", description = "通知相关API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {



}