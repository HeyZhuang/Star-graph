package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.UserLoginRequestDto;
import cn.itcast.star.graph.core.dto.UserLoginResponeDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
* <p>
* sg_user 控制器实现
* </p>
*
* @author luoxu
* @since 2024-10-15 14:47:14
*/
@RestController
@RequestMapping("/api/1.0/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<UserLoginResponeDto> login(@RequestBody UserLoginRequestDto dto){
        UserLoginResponeDto userLoginResponeDto = userService.loginByPassword(dto);
        return Result.ok(userLoginResponeDto);
    }

}
