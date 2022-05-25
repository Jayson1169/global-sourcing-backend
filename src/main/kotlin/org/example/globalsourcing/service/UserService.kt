package org.example.globalsourcing.service

import org.example.globalsourcing.entity.User
import org.example.globalsourcing.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    /**
     * 查询时的排序依据。
     */
    private val sort: Sort = Sort.by(Sort.Direction.ASC, "name", "role")

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username) ?: throw UsernameNotFoundException("用户'${username}'不存在！")
        val authorities = user.authorities
        authorities.add(SimpleGrantedAuthority("ROLE_${user.role.toString()}"))
        authorities.add(SimpleGrantedAuthority("ROLE_USER"))
        return user
    }

    /**
     * 新增用户，要求账号（username）不重复。
     */
    fun insert(user: User): User {
        val username = user.username!!
        if (userRepository.existsByUsername(username)) {
            throw ServiceException("账号为'${username}'的用户已存在!")
        }

        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user)
    }

    /**
     * 更新用户信息，要求更新后账号（username）不重复，
     * 且不能更新用户密码，需要更改密码参考 [updatePassword]。
     */
    fun update(user: User): User {
        val username = user.username!!
        val id = user.id
        val temp = userRepository.findById(id).orElseThrow { ServiceException("id为'${id}' 的用户不存在！") }

        // 有更新账号的情况，检查更新后账号是否重复
        if (temp.username != username && userRepository.existsByUsername(username)) {
            throw ServiceException("账号为'${username}'的用户已存在！")
        }

        user.password = temp.password
        return userRepository.save(user)
    }

    /**
     * 更新用户密码。
     */
    fun updatePassword(id: Long, password: String): User {
        val user: User = userRepository.findById(id).orElseThrow { ServiceException("id为'${id}' 的用户不存在！") }
        user.password = passwordEncoder.encode(password)
        return userRepository.save(user)
    }

    /**
     * 依据ID删除用户信息。
     */
    fun delete(id: Long) {
        userRepository.findById(id).ifPresent { userRepository.delete(it) }
    }

    /**
     * 查询用户，可选条件：用户角色 [role]。
     */
    fun findAll(role: User.Role?, page: Int, size: Int): Page<User> {
        return userRepository.findAll(
            { root, _, criteriaBuilder ->
                val predicates = mutableListOf<Predicate>()
                role?.let { predicates.add(criteriaBuilder.equal(root.get<User.Role>("role"), it)) }

                criteriaBuilder.and(*predicates.toTypedArray())
            }, PageRequest.of(page, size, sort)
        )
    }

    /**
     * 依据账号获取指定用户。
     */
    fun findByUsername(username: String): User {
        return userRepository.findByUsername(username) ?: throw ServiceException("不存在账号为'${username}'的用户！")
    }

    /**
     * 按账号或姓名搜索用户。
     */
    fun search(keyword: String, page: Int, size: Int): Page<User> {
        val pattern = "%${keyword}%"
        return userRepository.findAll(
            { root, _, criteriaBuilder ->
                criteriaBuilder.or(
                    criteriaBuilder.like(root.get("username"), pattern),
                    criteriaBuilder.like(root.get("name"), pattern)
                )
            }, PageRequest.of(page, size, sort)
        )
    }
}