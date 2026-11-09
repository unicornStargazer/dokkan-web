-- 停止同步后执行，保留全局版本数据；再次上线前恢复原表名。
RENAME TABLE kx_wiki_sync_checkpoint TO kx_wiki_sync_checkpoint_rollback;
