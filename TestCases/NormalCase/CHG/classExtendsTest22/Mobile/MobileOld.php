<?php

namespace com\framework\normal;

class Mobile {
    protected $message;
    
    public function __construct($message)
    {
        $this->message = $message;
    }

    public function getMessage()
    {
        return $this->message;
    }

    // ...
}