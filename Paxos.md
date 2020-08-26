

1.当A节点挖出矿时
    向每个节点发送一个prepare(chainHeight,blockHead)

2.然后其他节点接收到prepare(chainHeight,blockHead)时，
        if(chainHeight > lastHeight){
            if(check(blockHead.getHash())){
                return true(height,null);
            }
            else {
                return reject(lastHeight);
            }
        }   
        else if(chainHeight <= lastHeight){      
            return reject(lastHeight);
        }

3.当A收到信息         
        if(trueCount > half){
            sendAccept(chainHeight,block);
        }
        else if(trueCount <= half){
            return false;
        }
        
4.第二阶段其他节点收到accept(chainHeight,block)
        if(chainHeight > lastHeight ){
            update(this.blockchain,block);
        }