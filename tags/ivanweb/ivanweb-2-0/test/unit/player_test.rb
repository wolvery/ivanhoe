require File.dirname(__FILE__) + '/../test_helper'

class PlayerTest < Test::Unit::TestCase
  self.use_instantiated_fixtures  = true
  
  fixtures :player
    
  def test_auth  
    assert_equal  @bob, Player.authenticate("bob", "test")    
    assert_nil    Player.authenticate("nonbob", "test")

  end


  def test_passwordchange
        
    @longbob.change_password("nonbobpasswd")
    assert_equal @longbob, Player.authenticate("longbob", "nonbobpasswd")
    assert_nil   Player.authenticate("longbob", "longtest")
    @longbob.change_password("longtest")
    assert_equal @longbob, Player.authenticate("longbob", "longtest")
    assert_nil   Player.authenticate("longbob", "nonbobpasswd")
        
  end
  
  def test_disallowed_passwords

    u = Player.new    
    u.playername = "nonbob"

    u.password = u.password_confirmation = "tiny"
    assert !u.save     
    assert u.errors.invalid?('password')

    u.password = u.password_confirmation = "hugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehugehuge"
    assert !u.save     
    assert u.errors.invalid?('password')
        
    u.password = u.password_confirmation = ""
    assert !u.save    
    assert u.errors.invalid?('password')
        
    u.password = u.password_confirmation = "bobs_secure_password"
    assert u.save     
    assert u.errors.empty?
        
  end
  
  def test_bad_playernames

    u = Player.new  
    u.password = u.password_confirmation = "bobs_secure_password"

    u.playername = "x"
    assert !u.save     
    assert u.errors.invalid?('playername')
    
    u.playername = "hugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhugebobhug"
    assert !u.save     
    assert u.errors.invalid?('playername')

    u.playername = ""
    assert !u.save
    assert u.errors.invalid?('playername')

    u.playername = "okbob"
    assert u.save  
    assert u.errors.empty?
      
  end


  def test_collision
    u = Player.new
    u.playername      = "existingbob"
    u.password = u.password_confirmation = "bobs_secure_password"
    assert !u.save
  end


  def test_create
    u = Player.new
    u.playername      = "nonexistingbob"
    u.password = u.password_confirmation = "bobs_secure_password"
      
    assert u.save  
    
  end
  
  def test_sha1
    u = Player.new
    u.playername      = "nonexistingbob"
    u.password = u.password_confirmation = "bobs_secure_password"
    assert u.save
        
    assert_equal '98740ff87bade6d895010bceebbd9f718e7856bb', u.password
    
  end

  
end
