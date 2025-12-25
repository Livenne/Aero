package io.github.livenne;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseEntity {
    private Integer code;
    private String message;
    private Object data;

    public ResponseEntity(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResponseEntity ok(){
        return new ResponseEntity(ResponseStatus.SUCCESS,null,null);
    }

    public static ResponseEntity ok(Object data){
        return new ResponseEntity(ResponseStatus.SUCCESS,null,data);
    }

    public static ResponseEntity err(){
        return new ResponseEntity(ResponseStatus.ERROR,null,null);
    }

    public static ResponseEntity err(Object data){
        return new ResponseEntity(ResponseStatus.ERROR,null,data);
    }

    public static ResponseEntity notFound(){
        return new ResponseEntity(ResponseStatus.NOT_FOUND,null,null);
    }

    public static ResponseEntity notFound(Object data) {
        return new ResponseEntity(ResponseStatus.NOT_FOUND,null,data);
    }

    public static ResponseEntity unAuth() {
        return new ResponseEntity(ResponseStatus.UNAUTHORIZED,null,null);
    }

    public static ResponseEntity unAuth(Object data) {
        return new ResponseEntity(ResponseStatus.UNAUTHORIZED,null,data);
    }

    public static ResponseEntity notPermission() {
        return new ResponseEntity(ResponseStatus.NOT_PERMISSION,null,null);
    }

    public static ResponseEntity notPermission(Object data) {
        return new ResponseEntity(ResponseStatus.NOT_PERMISSION,null,data);
    }

    public ResponseEntity message(String message,Object... args){
        this.message = String.format(message, args);
        return this;
    }

    public ResponseEntity code(Integer code) {
        this.code = code;
        return this;
    }
}