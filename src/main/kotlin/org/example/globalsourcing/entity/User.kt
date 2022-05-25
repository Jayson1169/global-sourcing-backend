package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.example.globalsourcing.util.PASSWORD_PATTERN
import org.example.globalsourcing.util.PHONE_NUMBER_PATTERN
import org.example.globalsourcing.util.USERNAME_PATTERN
import org.example.globalsourcing.validation.groups.Insert
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * 用户基本信息。
 */
@Entity
class User : BaseEntity(), UserDetails {
    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = USERNAME_PATTERN, message = "用户名长度在6～18之间，不包含特殊字符")
    @get:JvmName("username_")
    @Column(nullable = false)
    var username: String? = null

    /**
     * 密码，密文存储。
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = PASSWORD_PATTERN, message = "密码必须包含数字和字母，且长度在6～18之间", groups = [Insert::class])
    @get:JvmName("password_")
    @Column(nullable = false)
    var password: String? = null

    /**
     * 姓名，长度2～20。
     */
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名必须在{min}～{max}个字符之间")
    @Column(nullable = false)
    var name: String? = null

    /**
     * 手机号。
     */
    @Pattern(regexp = PHONE_NUMBER_PATTERN, message = "手机号格式错误")
    @Column
    var phoneNumber: String? = null

    /**
     * 角色，可选值参考枚举类[Role]。
     */
    @NotNull(message = "角色不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role? = null

    @Transient
    @JsonIgnore
    val authorities: MutableSet<GrantedAuthority> = HashSet()

    /**
     * 角色枚举类。
     */
    enum class Role(val description: String) {
        ADMIN("管理员"),
        SALESPERSON("销售员"),
        BUYER("采购员"),
        TRANSPORTER("转运员"),
        WAREHOUSE_KEEPER("仓管员"),
        TREASURER("财务员");
    }

    override fun getUsername(): String = username!!

    override fun getPassword(): String = password!!

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = true

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = true

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true

    @JsonIgnore
    override fun isEnabled(): Boolean = true
}