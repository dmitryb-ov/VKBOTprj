package com;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import java.io.IOException;

public interface FunctionHandler {
    void handle() throws IOException, ClientException, ApiException;
}
