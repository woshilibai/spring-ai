package com.evermodel.service.agg.aidemo.controller;

import com.evermodel.service.agg.aidemo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatService chatService;

    @RequestMapping("/chat")
    public String chat(@RequestParam(defaultValue = "讲个笑话") String prompt) {
        return chatService.chat(prompt);
    }

    @RequestMapping(value = "/chat-stream",  produces = "text/html;charset=UTF-8")
    public Flux<String> chatStream(@RequestParam(defaultValue = "讲个笑话") String prompt) {
        return chatService.chatStream(prompt);
    }

    @RequestMapping(value = "/chat-memory",  produces = "text/html;charset=UTF-8")
    public Flux<String> chatMemory(@RequestParam String prompt,
                                   @RequestParam Integer chatId) {
        return chatService.chatMemory(prompt, chatId);
    }

//
//    curl --request POST \
//            --url http://127.0.0.1:8080/ai/multi-modal \
//            --header 'Accept: */*' \
//            --header 'Accept-Encoding: gzip, deflate, br' \
//            --header 'Connection: keep-alive' \
//            --header 'User-Agent: PostmanRuntime-ApipostRuntime/1.1.0' \
//            --header 'content-type: multipart/form-data' \
//            --form 'prompt=请识别图片里面的内容' \
//            --form 'files=@[object Object]' \
//            --form 'files=@[object Object]'
//
    @RequestMapping(value = "/multi-modal",  produces = "text/html;charset=UTF-8")
    public Flux<String> multiModal(@RequestParam String prompt
            , @RequestParam List<MultipartFile>  files){
        return chatService.multiModalChat(prompt, files);
    }

    //  ===============RAG==========================
    @RequestMapping("/upload-file")
    public void uploadFile(@RequestParam MultipartFile file,
                           @RequestParam Integer chatId) {
        chatService.uploadFile(file, chatId);
    }

    @RequestMapping(value = "/rag-chat",  produces = "text/html;charset=UTF-8")
    public Flux<String> ragChat(@RequestParam String prompt,
                                @RequestParam Integer chatId) {
        return chatService.ragChat(prompt, chatId);
    }


    @RequestMapping(value = "/tools-chat")
    public String toolsChat(@RequestParam String prompt,
                                  @RequestParam Integer chatId) {
        return chatService.toolsChat(chatId, prompt);
    }

    @RequestMapping(value = "/tools-chat-stream",  produces = "text/html;charset=UTF-8")
    public Flux<String> toolsChatStream(@RequestParam String prompt,
                            @RequestParam Integer chatId) {
        return chatService.toolsChatStream(chatId, prompt);
    }

}