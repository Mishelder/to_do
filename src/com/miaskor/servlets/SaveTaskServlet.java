package com.miaskor.servlets;

import com.miaskor.dto.SaveTaskDto;
import com.miaskor.entity.Client;
import com.miaskor.entity.Task;
import com.miaskor.mapper.json.JsonToSaveTaskDtoMapper;
import com.miaskor.mapper.json.TaskIdToJsonMapper;
import com.miaskor.service.SaveTaskService;
import com.miaskor.util.ControllersURIKeys;
import com.miaskor.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(ControllersURIKeys.SAVE)
public class SaveTaskServlet extends HttpServlet {

    private final SaveTaskService saveTaskService = SaveTaskService.getInstance();
    private final JsonToSaveTaskDtoMapper jsonToSaveTaskDtoMapper = JsonToSaveTaskDtoMapper.getInstance();
    private final TaskIdToJsonMapper taskIdToJsonMapper = TaskIdToJsonMapper.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var body = JsonUtil.parseBody(req);
        var saveTaskDto = jsonToSaveTaskDtoMapper.map(body);
        var client =(Client) req.getSession().getAttribute("client");
        saveTaskDto.setClientId(client.getId());
        var task = saveTaskService.saveTask(saveTaskDto);
        resp.getWriter().write(taskIdToJsonMapper.map(task));
    }
}