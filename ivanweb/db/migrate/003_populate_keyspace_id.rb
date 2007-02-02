class PopulateKeyspaceId < ActiveRecord::Migration
  def self.up
    execute "UPDATE keyspace SET id= 1 WHERE tablename='action';";
    execute "UPDATE keyspace SET id= 2 WHERE tablename='bookmarks';";
    execute "UPDATE keyspace SET id= 3 WHERE tablename='category';";
    execute "UPDATE keyspace SET id= 4 WHERE tablename='discussion';";
    execute "UPDATE keyspace SET id= 5 WHERE tablename='document';";
    execute "UPDATE keyspace SET id= 6 WHERE tablename='game';";
    execute "UPDATE keyspace SET id= 7 WHERE tablename='link_target';";
    execute "UPDATE keyspace SET id= 8 WHERE tablename='move';";
    execute "UPDATE keyspace SET id= 9 WHERE tablename='player';";
    execute "UPDATE keyspace SET id= 10 WHERE tablename='role';";
    execute "UPDATE keyspace SET id= 11 WHERE tablename='document_version';";
  end

  def self.down
  end
end
