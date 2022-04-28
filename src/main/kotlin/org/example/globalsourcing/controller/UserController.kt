package org.example.globalsourcing.controller

import org.example.globalsourcing.entity.User
import org.example.globalsourcing.service.UserService
import org.example.globalsourcing.util.PASSWORD_PATTERN
import org.example.globalsourcing.util.ResponseData
import org.example.globalsourcing.validation.groups.Insert
import org.example.globalsourcing.validation.groups.Update
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('ADMIN')")
@Validated
class UserController(private val userService: UserService) {

    @PostMapping("/insert")
    fun insert(@Validated(Insert::class) @RequestBody user: User): ResponseData<User> {
        return ResponseData.success(userService.insert(user))
    }

    @PutMapping("/update")
    fun update(@Validated(Update::class) @RequestBody user: User): ResponseData<User> {
        return ResponseData.success(userService.update(user))
    }

    @PutMapping("/updatePassword")
    fun updatePassword(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = PASSWORD_PATTERN, message = "密码必须包含数字和字母，且长度在6～18之间") password: String?
    ): ResponseData<User> {
        val user = userService.updatePassword(id!!, password!!)
        return ResponseData.success(user)
    }

    @DeleteMapping("/delete")
    fun delete(@NotNull(message = "ID不能为空") id: Long?): ResponseData<User> {
        userService.delete(id!!)
        return ResponseData.success(null)
    }

    @GetMapping("/findAll")
    fun findAll(
        @RequestParam(required = false) role: User.Role?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<User>> {
        val users = userService.findAll(role, page, size)
        return ResponseData.success(users)
    }

    @GetMapping("/findByUsername")
    fun findByUsername(@NotBlank(message = "账号不能为空") username: String?): ResponseData<User> {
        val user = userService.findByUsername(username!!)
        return ResponseData.success(user)
    }

    @GetMapping("/search")
    fun search(
        @NotBlank(message = "关键词不能为空") keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<User>> {
        val users = userService.search(keyword!!, page, size)
        return ResponseData.success(users)
    }
}