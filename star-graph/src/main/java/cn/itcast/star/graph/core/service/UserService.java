package cn.itcast.star.graph.core.service;


import cn.itcast.star.graph.core.dto.UserLoginRequestDto;
import cn.itcast.star.graph.core.dto.UserLoginResponeDto;
import cn.itcast.star.graph.core.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* <p>
* sg_user Service 接口
* </p>
*
* @author luoxu
* @since 2024-10-15 14:45:42
*/
public interface UserService extends IService<User> {

    UserLoginResponeDto loginByPassword(UserLoginRequestDto dto);
}