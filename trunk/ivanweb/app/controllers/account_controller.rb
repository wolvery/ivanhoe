class AccountController < ApplicationController
  model   :player
  layout  'scaffold'

  def login
    case @request.method
      when :post
        if session['user'] = Player.authenticate(@params['user_login'], @params['user_password'])

          flash['notice']  = "Login successful"
          redirect_back_or_default :action => "welcome"
        else
          @login    = @params['user_login']
          @message  = "Login unsuccessful"
      end
    end
  end
  
  def signup
    case @request.method
      when :post
        @user = User.new(@params['user'])
        
        if @user.save      
          session['user'] = Player.authenticate(@user.login, @params['user']['password'])
          flash['notice']  = "Signup successful"
          redirect_back_or_default :action => "welcome"          
        end
      when :get
        @user = User.new
    end      
  end  
  
  def delete
    if @params['id'] and session['user']
      @user = Player.find(@params['id'])
      @user.destroy
    end
    redirect_back_or_default :action => "welcome"
  end  
    
  def logout
    session['user'] = nil
  end
    
  def welcome
  end
  
end
