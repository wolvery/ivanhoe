class AccountController < ApplicationController
  layout  'main_frame'

  def login
    case request.method
      when :post
        if session['user'] = Player.authenticate(params['user']['login'], params['user']['password'])

          flash['notice']  = "Login successful"
          redirect_back_or_default :controller => "games", :action => "index"
        else
          @login    = params['user_login']
          @message  = "Login unsuccessful"
      end
    end
  end
  
  def signup
    case request.method
      when :post
        @user = Player.new(params['user'])
        
          # use ivanhoe's keyspace table for unique id
          keyspace = Keyspace.find( :first, :conditions => "tablename = 'player'" )
          @user.id = keyspace.next_value
          keyspace.next_value = keyspace.next_value + 1
          keyspace.save
        
        if @user.save      
          session['user'] = Player.authenticate(@user.playername, params['user']['password'])
          flash['notice']  = "Signup successful"
          redirect_back_or_default :controller => "games", :action => "index"          
        end
      when :get
        @user = Player.new
    end      
  end  
  
  def delete
    if params['id'] and session['user']
      @user = Player.find(params['id'])
      @user.destroy
    end
    redirect_back_or_default :controller => "games", :action => "index"
  end  
    
  def logout
    session['user'] = nil
    redirect_back_or_default :controller => "games", :action => "index"
  end
  
end
