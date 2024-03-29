package org.bighamapi.hmp.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bighamapi.hmp.pojo.Column;
import org.bighamapi.hmp.util.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.bighamapi.hmp.pojo.Channel;
import org.bighamapi.hmp.service.ChannelService;

import org.bighamapi.hmp.entity.PageResult;
import org.bighamapi.hmp.entity.Result;
import org.bighamapi.hmp.entity.StatusCode;
/**
 * 频道 控制器层
 * @author bighamapi
 *
 */
@RestController
@RequestMapping("/channel")
public class ChannelController {

	@Autowired
	private ChannelService channelService;

	private Logger LOG = LoggerFactory.getLogger(ChannelController.class);
	/**
	 * 查询全部数据
	 * @return
	 */
	@RequestMapping(method= RequestMethod.GET)
	public Result findAll(){
		return new Result(true,StatusCode.OK,"查询成功",channelService.findAll());
	}
	
	/**
	 * 根据ID查询
	 * @param id ID
	 * @return
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.GET)
	public Result findById(@PathVariable String id){
		return new Result(true,StatusCode.OK,"查询成功",channelService.findById(id));
	}


	/**
	 * 分页+多条件查询
	 * @param searchMap 查询条件封装
	 * @param page 页码
	 * @param size 页大小
	 * @return 分页结果
	 */
	@RequestMapping(value="/search/{page}/{size}",method=RequestMethod.POST)
	public Result findSearch(@RequestBody(required=false) Map searchMap , @PathVariable int page, @PathVariable int size){
		Page<Channel> pageList = channelService.findSearch(searchMap, page, size);
		return  new Result(true,StatusCode.OK,"查询成功",  new PageResult<Channel>(pageList.getTotalElements(), pageList.getContent()) );
	}

	/**
     * 根据条件查询
     * @param searchMap
     * @return
     */
    @RequestMapping(value="/search",method = RequestMethod.POST)
    public Result findSearch( @RequestBody Map searchMap){
        return new Result(true,StatusCode.OK,"查询成功",channelService.findSearch(searchMap));
    }
	
	/**
	 * 增加
	 * @param channel
	 */
	@RequestMapping(method=RequestMethod.POST)
	public Result add(@RequestBody Channel channel){
		Map<String,String> map = new HashMap<>();
		map.put("name",channel.getName());
		if((channel.getId() == null) && (channelService.findSearch(map).isEmpty())) {
			channelService.add(channel);
			LOG.info("添加频道（标签）："+channel.getName());
		}
		return new Result(true,StatusCode.OK,"增加成功");
	}
	
	/**
	 * 修改
	 * @param channel
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.PUT)
	public Result update(@RequestBody Channel channel, @PathVariable String id ){
		channel.setId(id);
		channelService.update(channel);
		LOG.info("修改频道（标签）："+channel.getName());
		return new Result(true,StatusCode.OK,"修改成功");
	}
	
	/**
	 * 删除
	 * @param id
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.DELETE)
	public Result delete(@PathVariable String id ){
		LOG.info("删除频道（标签）："+channelService.findById(id).getName());
		channelService.deleteById(id);
		return new Result(true,StatusCode.OK,"删除成功");
	}
	
}
