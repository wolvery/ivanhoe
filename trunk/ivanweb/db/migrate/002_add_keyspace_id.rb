class AddKeyspaceId < ActiveRecord::Migration
  def self.up
    add_column( :keyspace, :id, :integer )
  end

  def self.down
    remove_column( :keyspace, :id )
  end
end
