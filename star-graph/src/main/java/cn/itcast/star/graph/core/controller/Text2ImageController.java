package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.dto.UserLoginRequestDto;
import cn.itcast.star.graph.core.dto.UserLoginResponeDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.Text2ImageService;
import cn.itcast.star.graph.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* <p>
* 文生图接口
* </p>
*
* @author luoxu
* @since 2024-10-15 14:47:14
*/
@RestController
@RequestMapping("/api/authed/1.0/t2i")
public class Text2ImageController {

    @Autowired
    private Text2ImageService text2ImageService;

    @PostMapping("/propmt")
    public Result<Text2ImageResponeDto> propmt(@RequestBody Text2ImageReqDto text2ImageReqDto) throws Exception {
        Text2ImageResponeDto text2ImageResponeDto = text2ImageService.textToImage(text2ImageReqDto);
        return Result.ok(text2ImageResponeDto);
    }

}
